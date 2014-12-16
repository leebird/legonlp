MAKE_TEMP = make_tmp

all: init charniak_parser nltk_data lib_java clean_tmp

init:
	- mkdir $(MAKE_TEMP)
	- mkdir data
	- mkdir lib
	- mkdir lib/python
	- mkdir lib/java

# require python-dev, sudo apt-get install python-dev
charniak_parser:
	wget -O $(MAKE_TEMP)/charniak.tar.gz https://pypi.python.org/packages/source/b/bllipparser/bllipparser-2014.08.29b.tar.gz
	cd $(MAKE_TEMP) && tar -zxvf charniak.tar.gz 
	cd $(MAKE_TEMP)/bllipparser-2014.08.29b/ && python setup.py install --home=$(PWD) 
	wget -O $(MAKE_TEMP)/biomodel.tar.gz https://www.dropbox.com/s/wq3709rfl1u0581/biomodel.tar.gz?dl=0
	cd $(MAKE_TEMP) && tar -zxvf biomodel.tar.gz -C ../data/

nltk_data:
	python3 -m nltk.downloader punkt -d data/nltk_data 

lib_java:
	wget -O $(MAKE_TEMP)/java.tar.gz https://www.dropbox.com/s/askhb386rxzckdu/java.tar.gz?dl=0
	cd $(MAKE_TEMP) && tar -zxvf java.tar.gz -C ../lib/

clean_tmp:
	-rm -rf $(MAKE_TEMP)/

clean:
	-rm -rf data/
	-rm -rf lib/
	-rm -rf $(MAKE_TEMP)/