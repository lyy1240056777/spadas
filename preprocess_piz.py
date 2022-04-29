# coding=UTF-8
import pandas as pd
import os
import csv
import re

filePath =  "./"
miaFile = "mia_osm21_pois.csv"
pizFile = "piz_osm21_pois.csv"
str= "addr:street -> MacArthur Causeway)"
pattern = "(?<=-> ).*"
patternHasbrackets = "(?<=-> ).*?(?=\))"

dic = dict()
names = []
n=0

with open(filePath+pizFile,'r',encoding="utf-8") as myFile:
    csv.field_size_limit(500 * 1024 * 1024)
    lines=csv.reader(myFile)
    for line in lines:
        # one 
        if len(line)==6 and line[0].find("addr:state")!=-1: 
            lat = line[0].split("\t")[1]
            lon = line[0].split("\t")[0]
            street = re.search(pattern,line[1]).group()
            if names.count(street)==0:
                names.append(street)
            state = re.search(pattern,line[0].split("\t")[8]).group()
            country = line[2]
            postcode = line[3]
            housenumber=line[4]
            city=line[5]

            dic.setdefault(street+"-lat",[]).append(lat)
            dic.setdefault(street+"-lon",[]).append(lon)
            dic.setdefault(street+"-street",[]).append(street)
            dic.setdefault(street+"-postcode",[]).append(re.search(pattern,postcode).group())
            dic.setdefault(street+"-housenumber",[]).append(re.search(pattern,housenumber).group())
            dic.setdefault(street+"-state",[]).append(state)
            dic.setdefault(street+"-city",[]).append(re.search(patternHasbrackets,city).group())
            dic.setdefault(street+"-country",[]).append(re.search(pattern,country).group())
        # two
        elif len(line)==3 and line[0].find("addr:street")!=-1: 
            lat = line[0].split("\t")[1]
            lon = line[0].split("\t")[0]
            street = re.search(pattern,line[0].split("\t")[8]).group()
            if names.count(street)==0:
                names.append(street)
            postcode = line[1]
            housenumber = line[2]
            dic.setdefault(street+"-lat",[]).append(lat)
            dic.setdefault(street+"-lon",[]).append(lon)
            dic.setdefault(street+"-street",[]).append(street)
            dic.setdefault(street+"-postcode",[]).append(re.search(pattern,postcode).group())
            dic.setdefault(street+"-housenumber",[]).append(re.search(patternHasbrackets,housenumber).group())
            dic.setdefault(street+"-state",[]).append(" ")
            dic.setdefault(street+"-city",[]).append(" ")
            dic.setdefault(street+"-country",[]).append(" ")

for name in names:
    lats = dic.get(name+"-lat")
    lons = dic.get(name+"-lon")
    streets = dic.get(name+"-street")
    postcodes = dic.get(name+"-postcode")
    housenumbers = dic.get(name+"-housenumber")
    states=dic.get(name+"-state")
    citys = dic.get(name+"-city")
    countrys = dic.get(name+"-country")
    dfmap = {"lat":lats,"lon":lons,"street":streets,"postcode":postcodes,"housenumber":housenumbers,"city":citys,"state":states,"country":countrys}
    df = pd.DataFrame(dfmap)
    df.to_csv(filePath+"osm21_pois_"+name+".csv",index=False)
    print("ok")

print("Finally ok")
