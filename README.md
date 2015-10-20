Booksonic Client
===================

Booksonic aka Popeens DSub is a fork of the Subsonic fork DSub but with a twist.
While DSub and as far as I know all the other Subsonic forks out there are focused on music, Booksonic, as the name suggests is focused on audiobooks.

Under the hood Booksonic is pretty much vanilla DSub (why change what is already awsome?) but I aim at making the GUI quite different and better for Audiobooks. Great huh.
Booksonic also comes with a modified server that allows it to get more information about the book (album in subsonic terms) then a normal server. 
For now that means that you can get book descriptions inside the app, in the future it will probably mean a bit more.
Since I havent started working on anything other then the API for the servers I recommend you check the github page regularly if you decide to use the Booksonic server, any update notices you might get are for Subsonic, not Booksonic.

If you would rather keep the server you are using right now Booksonic will work with that too but you will lose some functionality.

So whats the catch? 

Well... When making the GUI better for audiobooks that means making it worse for music. Things like shuffle playlist for example that are essential for a music player are useless for an audiobook player and will be removed from the GUI.
This means that if you intend to listen to both audiobooks and music I would recommend you use Vanilla DSub for music and Booksonic for audiobooks.

Vanilla DSub: https://github.com/daneren2005/Subsonic

Booksonic Server: https://github.com/popeen/Popeens-Subsonic