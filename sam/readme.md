# AWS SAM

update my version of sam to the latest:

```sh
$ brew upgrade aws-sam-cli
$ sam --version
SAM CLI, version 0.48.0
```

## Create a Sample Application

```
$ sam init
...
select python 3.7 app and name it pwgenerator
...
$ cd pwgenerator
$ ls
README.md	events		hello_world	template.yaml	tests
```

Build and run the code
```bash
$ sam build --use-container
$ sam local invoke
# deploy
$ sam deploy --guided

```



$ which python
/anaconda3/bin/python

$ python --version
Python 3.7.0

$ /usr/local/bin/python3 --version
Python 3.6.5

$ which python3
/Library/Frameworks/Python.framework/Versions/3.6/bin/python3

$ python3 --version
Python 3.6.5

lrwxr-xr-x   1 root  wheel   24 May  8 12:43 Headers -> Versions/Current/Headers
lrwxr-xr-x   1 root  wheel   23 May  8 12:43 Python -> Versions/Current/Python
lrwxr-xr-x   1 root  wheel   26 May  8 12:43 Resources -> Versions/Current/Resources

(base) razing@mlb727 Python.framework $ ls Versions/3.7
Headers		Python		Resources	_CodeSignature	bin		etc		include		lib		share
