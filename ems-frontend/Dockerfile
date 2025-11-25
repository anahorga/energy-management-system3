# ----- 1. Faza de Build (Construire) -----
# Folosim o imagine oficială Node.js pentru a construi aplicația React
FROM node:20-alpine AS build

# Setăm directorul de lucru în container
WORKDIR /app

# Copiem fișierele de management al pachetelor
COPY package.json package-lock.json ./

# Instalăm dependențele
RUN npm install

# Copiem restul codului sursă
COPY . .

# Construim aplicația pentru producție (creează folderul /dist)
#
RUN npm run build

# ----- 2. Faza de Servire (Servire) -----
# Folosim o imagine Nginx ușoară pentru a servi fișierele statice
FROM nginx:alpine

# Copiem fișierele statice construite din faza de build
COPY --from=build /app/dist /usr/share/nginx/html

# Copiem fișierul de configurare Nginx personalizat (îl creăm la pasul 2)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expunem portul 80 (pe care ascultă Nginx în mod implicit)
EXPOSE 80