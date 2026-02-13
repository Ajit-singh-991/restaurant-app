FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist/customer-app/browser /usr/share/nginx/html/customer
COPY --from=build /app/dist/waiter-app/browser /usr/share/nginx/html/waiter
COPY --from=build /app/dist/kitchen-app/browser /usr/share/nginx/html/kitchen
COPY --from=build /app/dist/admin-app/browser /usr/share/nginx/html/admin
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
