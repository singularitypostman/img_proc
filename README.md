Getting started
===============

Download dependencies
	make update

Compile project
	make compile
or simply 
	make
 
Run shell with loaded libraries
	make start

Loading image (in erlang shell)
	img_proc:load("priv/g3.png").

Starting OTP app - not neccessary for now
	application:start(crypto).
	pplication:start(erl_img).
	application:start(img_proc).


TIPS
----

Pretty record displaying enabling
	rr(img_proc).
Unbound variable
	f(A).

KNOWN BUGS
----------

Erlang shell (at first) do not detect app module for autocompliting. 