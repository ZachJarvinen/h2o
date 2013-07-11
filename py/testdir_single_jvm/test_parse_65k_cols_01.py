import unittest
import random, sys, time, os
sys.path.extend(['.','..','py'])

import h2o, h2o_cmd, h2o_hosts, h2o_browse as h2b, h2o_import as h2i, h2o_exec as h2e

def write_syn_dataset(csvPathname, rowCount, colCount, SEED):
    # 8 random generatators, 1 per column
    r1 = random.Random(SEED)
    dsf = open(csvPathname, "w+")

    for i in range(rowCount):
        rowData = []
        for j in range(colCount):
            r = r1.randint(0,1)
            rowData.append(r)

        rowDataCsv = ",".join(map(str,rowData))
        dsf.write(rowDataCsv + "\n")

    dsf.close()

class Basic(unittest.TestCase):
    def tearDown(self):
        h2o.check_sandbox_for_errors()

    @classmethod
    def setUpClass(cls):
        global SEED
        SEED = random.randint(0, sys.maxint)

        # SEED = 
        random.seed(SEED)
        print "\nUsing random seed:", SEED
        localhost = h2o.decide_if_localhost()
        if (localhost):
            h2o.build_cloud(1,java_heap_GB=4)
        else:
            h2o_hosts.build_cloud_with_hosts()

    @classmethod
    def tearDownClass(cls):
        h2o.tear_down_cloud()

    def test_many_cols_with_syn(self):
        SYNDATASETS_DIR = h2o.make_syn_dir()
        tryList = [
            (100, 65000, 'cH', 5),
            ]

        ### h2b.browseTheCloud()
        for (rowCount, colCount, key2, timeoutSecs) in tryList:
            SEEDPERFILE = random.randint(0, sys.maxint)

            csvFilename = 'syn_' + str(SEEDPERFILE) + "_" + str(rowCount) + 'x' + str(colCount) + '.csv'
            csvPathname = SYNDATASETS_DIR + '/' + csvFilename

            print "Creating random", csvPathname
            write_syn_dataset(csvPathname, rowCount, colCount, SEEDPERFILE)

            parseKey = h2o_cmd.parseFile(None, csvPathname, key2=key2, timeoutSecs=30)
            print csvFilename, 'parse time:', parseKey['response']['time']
            print "Parse result['destination_key']:", parseKey['destination_key']

            # We should be able to see the parse result?
            inspect = h2o_cmd.runInspect(None, parseKey['destination_key'], timeoutSecs=30)
            h2o_cmd.infoFromInspect(inspect, csvPathname)
            print "\n" + csvPathname, \
                "    num_rows:", "{:,}".format(inspect['num_rows']), \
                "    num_cols:", "{:,}".format(inspect['num_cols'])

            # should match # of cols in header or ??
            self.assertEqual(inspect['num_cols'], colCount,
                "parse created result with the wrong number of cols %s %s" % (inspect['num_cols'], colCount))
            self.assertEqual(inspect['num_rows'], rowCount,
                "parse created result with the wrong number of rows (header shouldn't count) %s %s" % \
                (inspect['num_rows'], rowCount))


            if not h2o.browse_disable:
                h2b.browseJsonHistoryAsUrlLastMatch("Inspect")
                time.sleep(5)

if __name__ == '__main__':
    h2o.unit_main()