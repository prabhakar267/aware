import sys
sys.path.append("..")

import MySQLdb

from mysql_config import HOSTNAME, USERNAME, PASSWORD, DATABASE


db = MySQLdb.connect(
	host=HOSTNAME,
	user=USERNAME,
	passwd=PASSWORD,
	db=DATABASE
)
cursor = db.cursor()


with open("names", "r") as f:
	x = f.readlines()
	x = map(str.strip, x)
	
	for name in x:
		query = "INSERT INTO users (name) VALUES ('%s')" % (name)
		cursor.execute(query)
		db.commit()
		print name + "\n"