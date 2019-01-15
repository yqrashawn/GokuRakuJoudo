brew_watchexec="/usr/local/Cellar/watchexec/1.9.0/"
brew_lein="/usr/local/Cellar/leiningen/2.8.1/"
if [ -d "$brew_watchexec" ]; then
    ln -s "$brew_watchexec"bin/watchexec /usr/local/bin
else
    brew install watchexec
fi
if [ -d "$brew_lein" ]; then
    ln -s "$brew_lein"bin/lein /usr/local/bin
else
    brew install leiningen
fi
brew link leiningen
brew link watchexec