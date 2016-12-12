import json
import MySQLdb
from flask import Flask, request, render_template
from flask_cors import CORS


from utils import dist_between_coord, get_current_stats
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


###############
# USER ROUTES #
###############

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
	tags = request.form.get("tags")

	query = "INSERT INTO `messages` (user_id, message, lon, lat, tags) VALUES ('%d','%s','%f','%f','%s')" % (user_id, message, lon, lat, tags)

	try:
		cursor.execute(query)
		db.commit()
		message_id = cursor.lastrowid
		success = True
	except Exception, e:
		db.rollback()
		success = False

		print "\n\n\n\n"
		print str(e)
		print "\n\n\n\n"

	response_json = {
		"success" : success,
		"message_id" : message_id,
	}
	return json.dumps(response_json)


@app.route("/get-message", methods=["GET"])
def getmessage():
	lat = float(request.args.get("lat"))
	lon = float(request.args.get("lon"))
	tag = int(request.args.get("tags"))

	response_json = []
	if tag == 0:
		query = "SELECT messages.id, messages.message, messages.score, messages.timestamp, users.name, messages.lat, messages.lon, messages.tags \
			FROM `messages` INNER JOIN `users` ON messages.user_id = users.id \
			ORDER BY messages.timestamp ASC LIMIT 30"
	else:
		query = "SELECT messages.id, messages.message, messages.score, messages.timestamp, users.name, messages.lat, messages.lon, messages.tags \
			FROM `messages` INNER JOIN `users` ON messages.user_id = users.id \
			WHERE messages.tags LIKE '%%%d%%' \
			ORDER BY messages.timestamp ASC LIMIT 30" % (tag)

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
				'author' : '@' + row[4],
				'timestamp' : row[3].strftime('%d %b %I:%M %p').lower(),
				'tags' : row[7],
			}
			response_json.append(temp_obj)

		row = cursor.fetchone()

	return json.dumps(response_json)


@app.route("/message-vote/<int:message_id>/<string:score>", methods=["GET"])
def messagevote(message_id, score):
	query = "UPDATE messages SET score = score + %s WHERE id = '%d'" % (score, message_id)

	try:
		cursor.execute(query)
		db.commit()
		success = True
	except Exception, e:
		print "\n\n\n"
		print str(e)
		print "\n\n\n"

		db.rollback()
		success = False

	response_json = {
		"success" : success,
	}

	return json.dumps(response_json)


#############################
# HUMAN BODY GRAPHIC ROUTES #
#############################

@app.route("/current-index/", methods=["GET"])
def currentindex():
	scores = get_current_stats()
	response_json = []

	titles = ['Sulphur Dioxide', 'Oxides of Nitrogen', 'Carbon Monoxide', "Ozone", "Benzene", "Ammonia"]
	highs = [10, 70, 3, 60, 4.5, 50]
	lows = [8, 50, 2, 50, 3.5, 40]

	ctr = 0
	for title in titles:
		if scores[title] >= highs[ctr]:
			level = 3
		elif scores[title] <= lows[ctr]:
			level = 1
		else:
			level = 2

		temp_obj = {
			'title' : title,
			'score' : scores[title],
			'level' : level,
		}

		response_json.append(temp_obj)
		ctr += 1

	return json.dumps(response_json)


##################
# CONSOLE ROUTES #
##################

@app.route("/get-top-priorities/<int:number_of_messages>", methods=["GET"])
def gettoppriorities(number_of_messages):
	response_json = []

	query = "SELECT messages.id, messages.message, messages.score, messages.timestamp, users.name, messages.lat, messages.lon \
		FROM `messages` INNER JOIN `users` ON messages.user_id = users.id \
		ORDER BY messages.score DESC\
		LIMIT %d" % (number_of_messages)

	cursor.execute(query)
	row = cursor.fetchone()

	while row is not None:
		temp_obj = {
			'id' : int(row[0]),
			'message' : row[1],
			'score' : int(row[2]),
			'author' : row[4],
			'timestamp' : row[3].strftime('%m/%d/%Y'),
			'latitude' : float(row[5]),
			'longitude' : float(row[6]),
		}
		response_json.append(temp_obj)
		row = cursor.fetchone()

	return json.dumps(response_json)


@app.route("/get-all-messages", methods=["GET"])
def getallmessages():
	response_json = []

	query = "SELECT score, lat, lon \
		FROM `messages` \
		WHERE 1 \
		ORDER BY lat DESC"

	cursor.execute(query)
	row = cursor.fetchone()

	while row is not None:
		temp_obj = {
			'lat' : float(row[1]),
			'lon' : float(row[2]),
			'score' : int(row[0]),
		}
		response_json.append(temp_obj)

		row = cursor.fetchone()

	return json.dumps(response_json)


@app.route('/dashboard/heatmap', methods=["GET"])
def testroute():
	return render_template('heatmap.html')

@app.route('/dashboard/', methods=["GET"])
def dashboardindex():
	return render_template('admin.html')


##################
# DEFAULT ROUTES #
##################

@app.route('/', methods=['GET'])
def main():
	return "Hello world", 200


if __name__ == "__main__":
	app.run(debug=True, host="0.0.0.0", threaded=True)
