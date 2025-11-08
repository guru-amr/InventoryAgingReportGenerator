FROM nginx:alpine
COPY *.html *.css *.js *.csv *.xlsx /usr/share/nginx/html/
EXPOSE $PORT
CMD sed -i -e 's/$PORT/'"$PORT"'/g' /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'