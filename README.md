## Getting Started
#### Sign up for Pangdata.com ####
Before you begin, you need an Pangdata.com account. 
Please visit <a href="http://pangdata.com" target="_blank">https://pangdata.com</a> and create an account and retrieve your user key in user profile.

#### Minimum requirements ####
To run the application you will need **Java 1.5+**.

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
```bash
########################################
# Insert filesystem name
# ex)df.key.# = filesystem_name
########################################
df.key.1 = _PARTITION_NAME
########################################
``` 
Note: You can add multiple partitions using syntax 'df.key.[index]'.

###### Step 2-3: Confgiure processes in centos7.properties
```bash
########################################
# Insert process unique name
# ex)ps.key.# = process_name
########################################
ps.key.1 = _PROCESS_NAME
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
