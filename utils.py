import re
import requests
from datetime import datetime
from lxml import html, etree
from math import sqrt

def dist_between_coord(x1,y1,x2,y2):
	return sqrt( (x2 - x1)**2 + (y2 - y1)**2 )


# import time
# from pymongo import MongoClient, errors

# DB = MongoClient()['airData']
LOCATIONS = ['mm', 'av', 'pb', 'rkPuram', 'airpo', 'civilLines']
DATA_SOURCE_URL = 'http://www.dpccairdata.com/dpccairdata/display/'
LOCATION_NAME_FILE_POSTFIX = 'View15MinData.php'
# Reload rate = minutes * 60 (secs)
RELOAD_RATE = 15*60

def get_current_stats():
	for location in LOCATIONS:
		response = []
		response_obj = {}
		request_url = DATA_SOURCE_URL + location + LOCATION_NAME_FILE_POSTFIX
		source_response = requests.get(request_url)
		stringified_response = source_response.text
		dom_tree = html.fromstring(stringified_response)
		pollutants_table = dom_tree.xpath("//body/table/tr[4]/td/table/tr[6]/td/table/tr/td/table/tr")
		pollutants_table = pollutants_table[1:]
		for row in pollutants_table:
			row_string = html.tostring(row)
			again_ingest = html.fromstring(row_string)
			columns = again_ingest.xpath("//td")

			parsed_data = []

			for item in columns:
				parsed_data.append(item.text)

			parsed_data[3] = re.findall("\d+\.\d+", parsed_data[3])
			if len(parsed_data[3]) > 0:
				parsed_data[3] = float(parsed_data[3][0])
			else:
				parsed_data[3] = None

			if len(re.findall("-", parsed_data[4])) > 0:
				parsed_data[4] = None
			else:
				parsed_data[4] = int(re.findall("\d+", parsed_data[4])[0])

			date_string = parsed_data[1] + ' ' + parsed_data[2]
			date_obj = datetime.strptime(date_string, '%A, %B %d, %Y %X')
			parsed_data[5] = date_obj.strftime("%Y-%m-%d %H:%M:%S")
			response.append([parsed_data[0], parsed_data[3], parsed_data[4], parsed_data[5]])

		response_obj['_id'] = response[0][3] + ' ' + location
		for pollutant in response:
			response_obj[pollutant[0]] = pollutant[1]


		return response_obj

		# try:
		# 	DB.pollutionEntry.insert_one(response_obj)
		# except errors.DuplicateKeyError:
		# 	pass

	# time.sleep(RELOAD_RATE)
