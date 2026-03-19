FROM node:20-alpine AS development

WORKDIR /workspace
EXPOSE 5173
CMD ["sh", "-lc", "npm install && npm run dev -- --host 0.0.0.0 --port 5173"]

FROM node:20-alpine AS build

WORKDIR /build
COPY frontend/app/package.json ./package.json
RUN npm install
COPY frontend/app ./
RUN npm run build

FROM nginx:1.27-alpine AS runtime

COPY frontend/app/build/package/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/dist /usr/share/nginx/html
COPY frontend/app/build/package/config.template.js /usr/share/nginx/html/config.template.js
COPY frontend/app/build/package/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
EXPOSE 80
ENTRYPOINT ["/docker-entrypoint.sh"]
