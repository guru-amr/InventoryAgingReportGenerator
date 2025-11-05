FROM nginx:alpine
COPY *.html *.css *.js *.csv *.xlsx /usr/share/nginx/html/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]