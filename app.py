import json
import MySQLdb
from flask import Flask, request, redirect
from flask_cors import CORS


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
	user_id = int(request.args.get("user_id"))
	lon = float(request.args.get("lon"))
	lat = float(request.args.get("lat"))
	message = request.args.get("message")
	channel = request.args.get("channel")

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


@app.route("/get-messages", methods=["GET"])
def getmessage():
	lat = float(request.args.get("lat"))
	lon = float(request.args.get("lon"))
	channel = request.args.get("channel")

	ans = []
	query = "SELECT messages.id, messages.message, messages.score, messages.timestamp, users.name FROM `messages` INNER JOIN `users` ON messages.user_id = users.id WHERE messages.lon = '%f' AND messages.lat = '%f' AND messages.channel = '%s' ORDER BY messages.timestamp DESC" % (lat, lon, channel)

	try:
		cursor.execute(query)
		row = cursor.fetchone()

		while row is not None:
			ans.append(row)
			row = cursor.fetchone()

	except e:
		db.rollback()
		success = False

	# response_json = {
	# 	"success" : success,
	# 	"message_id" : message_id,
	# }
	return json.dumps(ans)




@app.route('/', methods=['GET'])
def main():
	return "Hello world", 200


if __name__ == "__main__":
	app.run(debug=True, host="0.0.0.0")