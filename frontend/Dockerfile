FROM node:18 as react-build

WORKDIR /app

COPY package.json .
RUN npm i -f
COPY . ./
RUN npm run build

FROM leanhtuan/nginx-with-brotli:latest

COPY default.conf /etc/nginx/nginx.conf
COPY --from=react-build /app/build /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
