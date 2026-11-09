#!/bin/bash
# Datos de usuario (User data) usados al lanzar la instancia EC2 "API-MiTiendita".
# Al primer arranque instala Node.js, descarga este repositorio y deja la API
# como servicio de systemd en el puerto 80 (HTTP).
export HOME=/root PORT=80
curl -fsSL https://raw.githubusercontent.com/agdisc11/SH01_Aplicaci-n-CRUD/main/backend/deploy_ec2.sh -o /root/deploy_ec2.sh
bash /root/deploy_ec2.sh > /var/log/mitiendita-deploy.log 2>&1
