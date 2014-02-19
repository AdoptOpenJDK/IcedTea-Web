# we need c99 because of snprintf function!
# (this function does not exist in C89/ANSI C)

softkiller:	softkiller.c
	$(CC) -Wall -pedantic -std=c99 -o $@ $< -lX11

clean:
	rm softkiller

