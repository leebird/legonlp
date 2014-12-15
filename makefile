


charniak_parser:
	python setup.py install --home=./

nltk_data:
	python3 -m nltk.downloader punkt -d data/nltk_data