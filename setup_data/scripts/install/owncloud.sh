sudo apt-get install -y owncloud-client-cmd

#create netrc file:
#'
#machine owncloud.domain.xyz
#login "user"
#password "password"
#'

#create exclude list
sudo mkdir /etc/owncloud-client
sudo touch /etc/owncloud-client/sync-exclude.lst