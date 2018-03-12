import osimport refrom collections import defaultdictimport mathimport mysql.connectorimport jsonimport nltkfrom bs4 import BeautifulSoupfrom nltk import WordNetLemmatizerimport timeclass MySQLInjection:    def __init__(self):        self.doc_id_url_dict = dict()        self.conn = mysql.connector.connect(user="mytestuser", password="mypassword", database="search")        self.cursor = self.conn.cursor()        self.term_tf = defaultdict()        self.total_doc = 0    def __exit__(self, exc_type, exc_val, exc_tb):        if self.conn:            self.conn.commit()            self.conn.close()    def build_DocID_URL_Dict(self, file):        add_doc_id_url = ("insert ignore into doc_id_url (id, url) values (%s, %s)")        data = json.load(open(file), "utf-8")        self.conn.autocommit=False        for id in data:            id = id.strip()            url = data[id].strip()            self.doc_id_url_dict[id] = url            data_query = [id, url]            self.cursor.execute(add_doc_id_url, data_query)            self.total_doc += 1        self.conn.commit()    def build_Term_DocID_Dict(self, doc_id, soup):        print "start to build index"        start_time= time.time()        self.conn.autocommit=False        lmtzr = WordNetLemmatizer()        add_term = ("insert ignore into terms (term) values (%s)")        add_term_docid = ("insert ignore into term_id_tf_tfidf (term_id,doc_id) values (%s, %s) on duplicate key update tf=tf+1 ")        for token in nltk.word_tokenize(soup):            lm_token = lmtzr.lemmatize(token)#            print lm_token            if len(lm_token) > 100:                continue            add_data_query = [lm_token, doc_id]            try:                self.cursor.execute(add_term, [lm_token])                self.cursor.execute(add_term_docid, add_data_query)            except mysql.connector.Error as e:                    # print e                    raise            except:                    raise        self.conn.commit()        print("--- %s seconds ---" % (time.time() - start_time))    def parse_url(self, file_path, file_id):        print "start to parse file"        start_time = time.time()        file = open(file_path).read()        # soup = BeautifulSoup(file, 'lxml').find_all(['title','p', 'li', 'div', 'a','h1','h2','h3','strong','em',\        #                                              'table','tr','td','th','b'])        # for i in soup:        #     print i.get_text()        soup = BeautifulSoup(file, 'lxml').get_text()        if len(soup) > 10000:            return        soup = re.sub(r'[^\x00-\x7F]+', '', soup.lower())        print("--- %s seconds ---" % (time.time() - start_time))        self.build_Term_DocID_Dict(file_id, soup)    def read_url_file(self, path):        sql = ("select * from doc_id_url")        self.cursor.execute(sql)        data = self.cursor.fetchall()        for row in data:            file_id = row[0]            url = row[1]            if url.endswith("txt") \                    or url.endswith(".r") \                    or url.endswith(".m") \                    or url.endswith(".java") \                    or url.endswith(".jpg") \                    or url.endswith(".py") \                    or url.endswith(".cc") \                    or url.endswith(".h") \                    or url.endswith(".cpp") \                    or url.endswith(".r") \                    or url.endswith(".m"):                continue            file_path = os.path.join(path, file_id)            # if file_id.startswith('0/'):            print "read file: ", file_path            self.parse_url(file_path, file_id)    def update_tfidf(self):        self.conn.autocommit=False        select_terms = ("select * from terms")        self.cursor.execute(select_terms)        count = self.cursor.rowcount        terms = self.cursor.fetchall()        for term, in terms:            # print term            select_posting_list = ("select * from term_id_tf_tfidf where term_id = %s")            self.cursor.execute(select_posting_list, [term])            posting_list = self.cursor.fetchall()            posting_list_length = self.cursor.rowcount            # print "posting length:", posting_list_length            for posting in posting_list:                # print "positing", posting                id = posting[1]                tf = posting[2]                tfidf = (float(math.log10(1 + tf)) * float(math.log10(float(self.total_doc) / posting_list_length)))                update_tfidf = ("update term_id_tf_tfidf set tfidf = %s where term_id = %s and doc_id = %s")                data_tfidf_query = [tfidf, term, id]                self.cursor.execute(update_tfidf, data_tfidf_query)        self.conn.commit()if __name__ == '__main__':    start_time = time.time()    builder = MySQLInjection()    builder.build_DocID_URL_Dict('bookkeeping.json')    builder.read_url_file('/Users/Wenhan/PycharmProjects/ics121/SearchEngine/WEBPAGES_RAW_ALL')    builder.update_tfidf()    print("--- %s seconds ---" % (time.time() - start_time))