#!/usr/bin/python3

import redis
import argparse

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


def expiry_1(is_test):
    if is_test == True:
        result = {
            "2020-08-21": 1597960800000,
            "2020-09-18": 1600380000000,
            "2020-10-16": 1602799200000,
            "2020-12-18": 1608246000000,
            "2021-03-19": 1616108400000,
            "2021-06-18": 1623967200000
        }
    else:
        result = {
            "2021-02-19": 1611874800000,
            "2021-03-19": 1616108400000,
            "2021-04-16": 1618524000000,
            "2021-06-18": 1623967200000,
            "2021-09-17": 1631829600000,
            "2021-12-17": 1639695600000
        }
    return result


def expiry_2(is_test):
    if is_test == True:
        result = {
            "2020-07-24": 1595541600000,
            "2020-07-31": 1596146400000,
            "2020-08-07": 1596751200000,
            "2020-08-14": 1597356000000,
        }
    else:
        result = {
            "2021-02-05": 1612479600000,
            "2021-02-12": 1613084400000,
            "2021-02-26": 1614294000000,
        }
    return result


def expiry_3(is_test):
    if is_test == True:
        result = [
            1618524000000,
            1621548000000,
            1623967200000,
            1631829600000,
            1639695600000,
            1671145200000
        ]
    else:
        result = [
            1618524000000,
            1621548000000,
            1623967200000,
            1631829600000,
            1639695600000,
            1671145200000
        ]
    return result


def init_redis(db):
    return redis.Redis(host='172.20.1.2', port=6379, db=db)


def populate_tickers(r):
    for k, v in TICKERS.items():
        print(k)
        r.hset("expiry", k, v)


def populate_expiry_x(ex, index, r):
    redis_key = "expiry-%d" % index
    for k, v in ex.items():
        r.hset(redis_key, k, v)


def populate_expiry(is_test, r):
    """
    populate_expiry_x(expiry_1(is_test), 1, r)
    populate_expiry_x(expiry_2(is_test), 2, r)

    print(r.hgetall("expiry"))
    print(r.hgetall("expiry-1"))
    print(r.hgetall("expiry-2"))
    """
    redis_key = "expiry"
    vals = expiry_3(is_test)
    for v in vals:
        r.sadd(redis_key, v)
    print(r.smembers(redis_key))


def populate_opening_prices(r):
    redis_key = "openingprices"
    r.hset(redis_key, "EQNR", "131.00")


# def populate(is_test, flush_all, add_tickers):

def populate_splu(r):
    redis_key = "splu"
    r.hset(redis_key, "EQNR", "1620594773")


def populate(args):
    if args.is_test == True:
        db = 5
    else:
        db = 0
    r = init_redis(db)
    if args.flush_all == True:
        r.flushall()
    """
    if args.add_tickers == True:
        populate_tickers(r)
    """
    if args.add_expiry == True:
        populate_expiry(args.is_test, r)
    if args.add_opening_prices == True:
        populate_opening_prices(r)
    if args.add_splu == True:
        populate_splu(r)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Populate Redis cache.')
    #parser.add_argument('integers', metavar='N', type=int, nargs='+',help='an integer for the accumulator')
    parser.add_argument('--test', dest='is_test', action='store_true',
                        # const=sum,
                        default=False, help='Is test. default: false')

    parser.add_argument('--flushall', dest='flush_all', action='store_true',
                        default=False, help='Flush Redis cache before populate. default: false')

    parser.add_argument('--tickers', dest='add_tickers', action='store_true',
                        default=False, help='Populate tickers. default: false')

    parser.add_argument('--expiry', dest='add_expiry', action='store_true',
                        default=False, help='Populate all expiry dates. default: false')

    parser.add_argument('--opening', dest='add_opening_prices', action='store_true',
                        default=False, help='Populate opening prices. default: false')

    parser.add_argument('--opening', dest='add_splu', action='store_true',
                        default=False, help='Populate splu (stockprices last updated). default: false')

    args = parser.parse_args()
    populate(args)
