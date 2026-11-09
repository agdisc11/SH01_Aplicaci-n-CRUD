#!/usr/bin/env bash
# Instala y deja corriendo la API de Mi Tiendita en una instancia EC2
# (Amazon Linux 2023 o Ubuntu). Se puede volver a ejecutar sin problema.
#
# Uso, desde la terminal de la instancia (EC2 Instance Connect o SSH):
#   curl -fsSL https://raw.githubusercontent.com/agdisc11/SH01_Aplicaci-n-CRUD/main/backend/deploy_ec2.sh | bash
set -euo pipefail

REPO_URL="${REPO_URL:-https://github.com/agdisc11/SH01_Aplicaci-n-CRUD.git}"
PORT="${PORT:-3000}"
APP_DIR="/opt/mitiendita"
RUN_USER="$(id -un)"

echo "==> 1/4 Instalando Node.js y git"
if command -v dnf >/dev/null 2>&1; then
  sudo dnf install -y nodejs git
  command -v npm >/dev/null 2>&1 || sudo dnf install -y nodejs-npm
elif command -v apt-get >/dev/null 2>&1; then
  sudo apt-get update -y
  sudo apt-get install -y nodejs npm git
else
  echo "Sistema no soportado: se esperaba Amazon Linux o Ubuntu." >&2
  exit 1
fi
echo "Node $(node --version)"

echo "==> 2/4 Descargando el proyecto en $APP_DIR"
if [ -d "$APP_DIR/.git" ]; then
  sudo git -C "$APP_DIR" pull --ff-only || echo "No se pudo actualizar; se usa la versión existente."
else
  sudo git clone --depth 1 "$REPO_URL" "$APP_DIR"
fi
sudo chown -R "$RUN_USER":"$RUN_USER" "$APP_DIR"

echo "==> 3/4 Instalando dependencias de la API"
cd "$APP_DIR/backend"
[ -f db.json ] || cp db.seed.json db.json
npm install --no-audit --no-fund

echo "==> 4/4 Creando el servicio 'mitiendita-api' (arranca solo al encender la instancia)"
sudo tee /etc/systemd/system/mitiendita-api.service >/dev/null <<UNIT
[Unit]
Description=API Mi Tiendita (json-server + json-server-auth)
After=network.target

[Service]
User=$RUN_USER
WorkingDirectory=$APP_DIR/backend
ExecStart=$APP_DIR/backend/node_modules/.bin/json-server-auth db.json --port $PORT --host 0.0.0.0 --routes routes.json
Restart=always

[Install]
WantedBy=multi-user.target
UNIT
sudo systemctl daemon-reload
sudo systemctl enable mitiendita-api >/dev/null 2>&1
sudo systemctl restart mitiendita-api
sleep 3

if curl -s -o /dev/null -w "%{http_code}" "http://localhost:$PORT/products" | grep -q 401; then
  PUBLIC_IP="$(curl -s --max-time 3 https://checkip.amazonaws.com || true)"
  echo
  echo "LISTO: la API responde en el puerto $PORT."
  echo "URL para la app: http://${PUBLIC_IP:-<IP-publica>}:$PORT/"
  echo "Recuerda abrir el puerto $PORT (TCP) en el grupo de seguridad de la instancia."
else
  echo "La API no respondió. Revisa el registro con: sudo journalctl -u mitiendita-api -n 50" >&2
  exit 1
fi
