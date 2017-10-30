# Aware (Server)
## Setup
First, clone the repo
```bash
git clone https://github.com/prabhakar267/aware.git && cd aware/server
```
Create a Python 2.7 virtual environment and activate it
```bash
pip install virtualenv
virtualenv venv
source venv/bin/activate
```
Install all of the Python 2.7 dependencies
```
[sudo] pip install -r requirements.txt
```
+ Edit [mysql_config.py.sample](mysql_config.py.sample) according to your MySQL configurations and rename it to mysql_config.py
+ Open your MySQL console / MySQL administration tool (like [phpMyAdmin](https://www.phpmyadmin.net/)) and import the [database dump](database/schema.sql)
Run the development server, which defaults to port 5000
```
python app.py
```
Visit the homepage  
**Open ```localhost:5000```**

## Available Routes

| Path | Request Type | Accepted Parameters | Response Type |
|---|---|---|---|
| ```/``` | GET | None | HTML |
|```/add-user``` | GET | **name** : string <br/> **password** : string | JSON |
|```/add-message``` | POST | **user_id** : integer <br/> **lon** : float <br/> **lan** : float <br/> **message** : string <br/> **tags** : list | JSON |
|```/get-message``` | GET | **lat** : float <br/> **lon** : float <br/> **tag** : list | JSON |
|```/message-vote/<message_id>/<score>``` | GET | None | JSON |
|```/current-index``` | GET | None | JSON |
|```/get-top-priorities/<number_of_messages>``` | GET | None | JSON |
|```/get-all-messages``` | GET | None | JSON |
|```/dashboard``` | GET | None | HTML |
|```/dashboard/heatmap``` | GET | None | HTML |

## Adding Fake Data
```bash
cd random\ data/
python put_*
```

