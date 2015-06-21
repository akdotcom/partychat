# Installation #

  1. [Download Eclipse](http://www.eclipse.org/downloads/) (Java IDE version)
  1. Install [subclipse](http://subclipse.tigris.org/servlets/ProjectProcess?pageID=p4wYuA) for SVN port
    1. **Help** menu -> **Install New Software...**
    1. Add a new source `http://subclipse.tigris.org/update_1.6.x`
    1. Install all components

# Getting the code #

  1. **Window** menu -> **Open Perspective** -> **SVN Repository Exploring**
  1. Add a repository with the URL `https://partychat.googlecode.com/svn`
  1. Check out the `trunk/partychat` directory

# Running #
  1. **Run** menu -> **Run configurations...**
  1. Under **Java Application** select **PartyBot**
  1. In the **Arguments** tab, enter (at least) three arguments
    * botname: can be anything
    * username: if just a username, will be considered to be a `gmail.com` Jabber account, otherwise everything after the @ sign is the Jabber server
    * password: that account's password
  1. Multiple username/password pairs may be repeated to connect to multiple bots