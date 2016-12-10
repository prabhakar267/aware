import json
import MySQLdb
from flask import Flask, request, redirect


from config import HOSTNAME, USERNAME, PASSWORD, DATABASE


app = Flask(__name__)

db = MySQLdb.connect(
	host=HOSTNAME,
	user=USERNAME,
	passwd=PASSWORD,
	db=DATABASE
)

cursor = db.cursor()

@app.route("/", methods=["GET"])
def main():
	user_id = int(request.args.get("id"))

	user_json = {
		"user_id" : user_id,
	}
	return json.dumps(user_json)


if __name__ == "__main__":
	app.run(debug=True)