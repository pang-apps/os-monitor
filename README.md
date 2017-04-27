# Monitoring your OS on mobile where ever you are.
OS monitoring application for performance and prediction of failure.
OS's every information can be monitored and analzed with Pangdata.com

###### Pangdata Cloud Monitoring Service  ######
<img src="https://github.com/pang-apps/os-monitor/blob/master/screen-shot/osmonitor-service.JPG" width="800" />

# Pang Data Cloud based Monitoring Service
This application used Pangdata.com cloud service. Pangdata.com is cloud based monitoring and analysis SaaS platform. 
You can monitor your cloud infrstructure and applications using <a href="https://github.com/pangdata/pang-sdk-java">Pang SDK

## Screen shot
###### Realtime monitoring dashboard ######
<img src="https://github.com/pang-apps/os-monitor/blob/master/screen-shot/dashboard.png" width="600" />

###### Realtime monitoring dashboard on Mobile ######
<img src="https://github.com/pang-apps/os-monitor/blob/master/screen-shot/dashboard-mobile.png" width="300" />

###### Realtime monitoring devices ######
<img src="https://github.com/pang-apps/os-monitor/blob/master/screen-shot/devices.png" width="600" />

###### Realtime monitoring devices on Mobile ######
<img src="https://github.com/pang-apps/os-monitor/blob/master/screen-shot/devices-m.png" width="300" />

## Getting Started
#### Sign up for Pangdata.com ####
Before you begin, you need an Pangdata.com account. 
Please visit <a href="http://pangdata.com" target="_blank">https://pangdata.com</a> and create an account and retrieve your user key in user profile.

#### Minimum requirements ####
To run the application you will need **Java 1.5+**.

#### Supported OS ####
* CentOS


#### Installation ####
Very easy to install ^^.

##### Step 1 #####

###### Windows ######
Download a <a href="https://github.com/pang-apps/os-monitor/releases/latest">OS monitoring application</a> file and unzip it.

###### Linux ######
``` 
wget https://github.com/pang-apps/os-monitor/releases/download/v1.0/os-monitor.tar && tar -xvf os-monitor.tar
``` 
##### Step 2: Configure pang.properties file #####
cd os-monitor/conf

###### Step 2-1: Confgiure your account and user key in pang.properties ######
```bash
#pangdata reserved properties
pang.username=_USERNAME
pang.userkey=_USER_KEY
pang.prefix=_PREFIX
pang.conf=centos7.properties
#Insert default period(seconds)
pang.period=15
``` 
Note: User key can be found in your profile of Pangdata.com

###### Step 2-2: Confgiure partition in centos7.properties
type "df"
```bash
[prever@localhost ~]$ df
Filesystem              1K-blocks     Used Available Use% Mounted on
/dev/mapper/centos-root  52403200 31136596  21266604  60% /
devtmpfs                  3942648        0   3942648   0% /dev
tmpfs                     3953076        0   3953076   0% /dev/shm
tmpfs                     3953076   459740   3493336  12% /run
tmpfs                     3953076        0   3953076   0% /sys/fs/cgroup
/dev/sda2                  505580   204712    300868  41% /boot
/dev/sda1                  204580     9640    194940   5% /boot/efi
/dev/mapper/centos-home 914972156  2223228 912748928   1% /home
tmpfs                      790616        0    790616   0% /run/user/997
tmpfs                      790616        0    790616   0% /run/user/1003
tmpfs                      790616        0    790616   0% /run/user/1002
tmpfs                      790616        0    790616   0% /run/user/1000
``` 

get filesystem unique name and config it
```bash
########################################
# Insert filesystem name
# ex)df.key.# = filesystem_name
########################################
df.key.1 = centos-root
df.key.2 = centos-home
########################################
``` 
Note: You can add multiple partitions using syntax 'df.key.[index]'.

###### Step 2-3: Confgiure processes in centos7.properties

ps -e -o cmd
```bash
########################################
# Insert process unique name
# ex)ps.key.# = process_name
########################################
ps.key.1 = centos-root
ps.key.2 = centos-home
########################################
``` 
Note: You can add multiple processes using syntax 'ps.key.[index]'.


##### Step 3: Run #####
###### Windows ######
``` 
os-monitor/pang.bat
``` 
###### Linux ######
Process will be launched in background.
``` 
os-monitor/pang.sh start
``` 
Check application's log
``` 
os-monitor/log.sh
``` 
##### Step 4: Access your devices #####
See your device in Pangdata.com

Login your account.
See main dashborad and you can find registered device.
Create your own dashboard and you can see realtime of your data.

##### Step 5: You are happy to play with your data #####
Wow!! all done. Enjoy and play with your device and your data.

##### Feel our demo #####
Go to https://pangdata.com
You can login using demo username 'pang-demo' with password 'panggood'.
