#! /usr/bin/python

import re
import time
import requests
from datetime import datetime
from lxml import html, etree

DATA_SOURCE = 'http://www.dpccairdata.com/dpccairdata/display/mmView15MinData.php'
# Reload rate = minutes * 60 (secs)
RELOAD_RATE = 15*60

while True:
    response = []
    source_response = requests.get(DATA_SOURCE)
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

        parsed_data[3] = float(re.findall("\d+\.\d+", parsed_data[3])[0])
        if len(re.findall("-", parsed_data[4])) > 0:
            parsed_data[4] = None
        else:
            parsed_data[4] = int(re.findall("\d+", parsed_data[4])[0])

        date_string = parsed_data[1] + ' ' + parsed_data[2]
        date_obj = datetime.strptime(date_string, '%A, %B %d, %Y %X')
        parsed_data[5] = date_obj.strftime("%Y-%m-%d %H:%M:%S")
        response.append([parsed_data[0], parsed_data[3], parsed_data[4], parsed_data[5]])

    print response

    time.sleep(RELOAD_RATE)
