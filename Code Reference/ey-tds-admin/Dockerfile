FROM nginx
RUN apt-get update \
    && apt-get -y install curl \
      && apt-get -y install vim 
# RUN rm -rf /usr/share/nginx/html/*
COPY default.conf /etc/nginx/conf.d/default.conf
COPY dist/ /usr/share/nginx/html
# VOLUME [ "/data" ]
EXPOSE 80
RUN service nginx start
CMD ["nginx", "-g", "daemon off;"]