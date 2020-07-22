#!/usr/bin/python3

import redis

TICKERS = {
    "AKERBP": 1,
    "AKSO": 1,
    # "BAKKA": 1,
    "BWLPG": 1,
    "DNB": 2,
    "DNO": 1,
    "EQNR": 2,
    "GJF": 1,
    # "GOGL": 1,
    # "MHG",
    "NAS": 2,
    "NHY": 2,
    # "OBX": 1,
    "ORK": 1,
    "PGS": 2,
    "REC": 1,
    # "SDRL": 1,
    "STB": 1,
    "SUBC": 2,
    "TEL": 1,
    "TGS": 1,
    # "TOM": 1,
    "YAR": 1,
}

EXPIRY_1 = {
    "2020-08-21": 1597960800000,
    "2020-09-18": 1600380000000,
    "2020-10-16": 1602799200000,
    "2020-12-18": 1608246000000,
    "2021-03-19": 1616108400000,
    "2021-06-18": 1623967200000
}

EXPIRY_2 = {
    "2020-07-24": 1595541600000,
    "2020-07-31": 1596146400000,
    "2020-08-07": 1596751200000,
    "2020-08-14": 1597356000000
}

EXPIRY_3 = {
    "2021-12-17": 1639695600000,
    "2022-06-17": 1655416800000
}

r = redis.Redis(
    host='172.20.1.2', port=6379, db=5
)


def populate_tickers():
    for k, v in TICKERS.items():
        print(k)
        r.hset("expiry", k, v)


def populate_expiry_x(ex, index):
    redis_key = "expiry-%d" % index
    for k, v in ex.items():
        r.hset(redis_key, k, v)

    """
    for v in ex:
        print(v)
        r.sadd(key, v)
    """


def populate_expiry():
    populate_expiry_x(EXPIRY_1, 1)
    populate_expiry_x(EXPIRY_2, 2)
    # populate_expiry_x(EXPIRY_3, 3)

    print(r.hgetall("expiry"))
    print(r.hgetall("expiry-1"))
    print(r.hgetall("expiry-2"))
    # print(r.hgetall("expiry-3"))


r.flushall()
populate_tickers()
populate_expiry()
