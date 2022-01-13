# conversion-rate-full

How to make Postgresql running on host accessible to pods running inside minikube vm ?
1. Make postgresql available for either all ip addresses, or for the ip address of host and minikube vm bridge. For this we need to update Postgresql configuration in file postgresql.conf by setting listen_addresses = "*"

2. Update Postgresql client authetication settings to allow minikube pod app to connect to it, by changing pg_hba.conf. 

3. For above both steps, we need to know host and minikube ip addresses for the bridge they are connected through.

## Create kubernetes secret for database credentials

kubectl create secret generic database-creds --from-literal=username=<dbusername> --from-literal=password=<dbpassword>

## Create kubernetes secret for play-application secret

kubectl create secret generic play-app-secret --from-literal=appkey=<thesecret>