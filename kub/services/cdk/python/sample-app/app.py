#!/usr/bin/env python3

from aws_cdk import core

from hello.hello_stack import MyStack


app = core.App()
MyStack(app, "baldur-cdk", env={'region': 'us-east-1'})

app.synth()
