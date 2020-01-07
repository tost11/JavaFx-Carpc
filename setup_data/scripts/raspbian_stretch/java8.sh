#!/bin/bash

apt-get install dirmngr

echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | sudo tee /etc/apt/sources.list.d/webupd8team-java.list
echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | sudo tee -a /etc/apt/sources.list.d/webupd8team-java.list
sudo apt-key adv --recv-key --keyserver keyserver.ubuntu.com EEA14886

apt-get update

echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | debconf-set-selections
echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 seen true" | debconf-set-selections

apt-get install --allow-unauthenticated -y oracle-java8-installer
apt-get install -y oracle-java8-set-default

java -version
