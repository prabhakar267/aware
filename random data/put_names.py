import MySQLdb

db = MySQLdb.connect(
	host="localhost",
	user="root",
	passwd="696163",
	db="csinseew_clean_air"
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
	# print x
