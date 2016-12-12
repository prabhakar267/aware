import random
import MySQLdb
from loremipsum import get_sentences

db = MySQLdb.connect(
	host="localhost",
	user="root",
	passwd="696163",
	db="csinseew_clean_air"
)
cursor = db.cursor()


tag_choices = ['134', '25', '12', '1345', '15', '23', '245']

# x = f.readlines()
# x = map(str.strip, x)

ctr = 0
while ctr < 20000:
	message = get_sentences(1)[0]
	score = random.randint(10, 20)
	user_id = random.randint(1, 100)
	r1 = random.random() * 2
	r2 = random.random() * 2

	if random.random() % 2 == 0:
		mode = 1
	else:
		mode = -1


# , , 

	lat = 28.541297 + (mode * r1)
	lon = 77.146554 + (mode * r2)

	tags = random.choice(tag_choices)

	# 28.7041, 77.1025
	query = "INSERT INTO messages (user_id, message, lat, lon, tags, score) VALUES ('%d','%s','%f','%f','%s','%d')" % (user_id, message, lat, lon, tags, score)
	ctr += 1
	cursor.execute(query)
	db.commit()
	print message + "\n"
	# print x
# 