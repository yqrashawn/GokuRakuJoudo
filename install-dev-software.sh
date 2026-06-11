#!/bin/bash

# Install leiningen for clojure
if ! command -v lein &>/dev/null; then
  echo "Installing leiningen..."
  brew install leiningen
else
  echo "leiningen is already installed"
fi

# Install java and nativeimage for creating release binaries
if ! command -v java &>/dev/null; then
  echo "java 21.0.5-graal is not installed"

  if [ ! -d "$HOME/.sdkman" ]; then
    echo "Installing sdkman..."
    curl -s "https://get.sdkman.io" | bash
    source "$HOME/.sdkman/bin/sdkman-init.sh"
  else
    echo "sdkman is already installed"
  fi

  echo "Installing java 21.0.5-graal..."
  sdk install java 21.0.5-graal
else
  echo "java 21.0.5-graal is already installed"
fi

if ! command -v native-image &>/dev/null; then
  echo "Installing nativeimage..."
  sdk install nativeimage
else
  echo "nativeimage is already installed"
fi
