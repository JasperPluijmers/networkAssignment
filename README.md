# networkAssignment

Thank you for checking out this repo. This is a fileserver/client written in Java. 
To use this Server or Client open the .jar in one of the following ways:

To start a server give the home directory from which clients may download, for example: \
"java -jar jarfile.jar -s /home/user/folder/" \
"When starting a client, give the download directory: \
"java -jar jarfile.jar -c /home/user/downloadFolder/"



The tui should guide you through the process of downloading/uploading files.
This software was tested mostly on linux, I give no guarantees on how well this works
with windows filesystems. Also the tui uses ANSI characters so if your terminal does
not suppor them, some things will look weird.
