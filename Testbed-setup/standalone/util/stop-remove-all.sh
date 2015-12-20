sudo docker kill `sudo docker ps -a |awk '{print $1 }'`
sudo docker rm `sudo docker ps -a |awk '{print $1}'`
sudo docker ps -a
