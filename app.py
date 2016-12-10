import json
import MySQLdb
from flask import Flask, request, redirect
from flask_cors import CORS


from utils import *
from mysql_config import HOSTNAME, USERNAME, PASSWORD, DATABASE


app = Flask(__name__)
CORS(app)


db = MySQLdb.connect(
	host=HOSTNAME,
	user=USERNAME,
	passwd=PASSWORD,
	db=DATABASE
)

cursor = db.cursor()

FIXED_DISTANCE = 1000


@app.route("/add-user", methods=["GET"])
def adduser():
	user_name = request.args.get("name")
	password = request.args.get("password")

	query = "INSERT INTO `users` (name) VALUES ('%s')" % (user_name)


	try:
		cursor.execute(query)
		db.commit()
		user_id = cursor.lastrowid
		success = True
	except e:
		db.rollback()
		success = False

	response_json = {
		"success" : success,
		"user_id" : user_id,
		"password" : password,
	}
	return json.dumps(response_json)


@app.route("/add-message", methods=["POST"])
def addmessage():
	user_id = int(request.form.get("user_id"))
	lon = float(request.form.get("lon"))
	lat = float(request.form.get("lat"))
	message = request.form.get("message")
	channel = request.form.get("channel")

	query = "INSERT INTO `messages` (user_id, message, lon, lat, channel) VALUES ('%d','%s','%f','%f','%s')" % (user_id, message, lon, lat, channel)

	try:
		cursor.execute(query)
		db.commit()
		message_id = cursor.lastrowid
		success = True
	except e:
		db.rollback()
		success = False

	response_json = {
		"success" : success,
		"message_id" : message_id,
	}
	return json.dumps(response_json)


@app.route("/get-message", methods=["GET"])
def getmessage():
	lat = float(request.args.get("lat"))
	lon = float(request.args.get("lon"))
	channel = request.args.get("channel")

	response_json = []
	query = "SELECT messages.id, messages.message, messages.score, messages.timestamp, users.name, messages.lat, messages.lon \
		FROM `messages` INNER JOIN `users` ON messages.user_id = users.id \
		WHERE messages.channel='%s' \
		ORDER BY messages.timestamp DESC" % (channel)

	cursor.execute(query)
	row = cursor.fetchone()

	while row is not None:
		
		message_lat = row[5]
		message_lon = row[6]
		message_score = int(row[2])

		approved_distance = FIXED_DISTANCE + message_score

		if(dist_between_coord(lat, lon, message_lat, message_lon) < approved_distance):
			temp_obj = {
				'id' : int(row[0]),
				'message' : row[1],
				'score' : message_score,
				'author' : row[4],
				'timestamp' : row[3].strftime('%m/%d/%Y'),
			}
			response_json.append(temp_obj)

		row = cursor.fetchone()

	return json.dumps(response_json)


@app.route('/', methods=['GET'])
def main():
	return "Hello world", 200


if __name__ == "__main__":
	app.run(debug=True, host="0.0.0.0")