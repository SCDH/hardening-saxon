# Hardening Saxon

Running XSLT, XQuery and XPath bears some security risks if you do not
know exactly what is in a script. For example, an XPath expression may
contain code for stealing secrets from your computer, or an XSLT
stylesheet may contain code for adding a login key to your ssh
configuration. This project aims to make
[Saxon](https://www.saxonica.com/) robust against such attacks by
adding control over the access to your local file system. It does so
by

- restricting file system access through `doc(...)`, `unparsed-text(...)`
  etc., and `<xsl:result-document>` to a set of allowed paths only,
  which is defined by configuration


## Getting started

Clone this repo and run `{shell}./mvnw package` (Linux/Mac) or
`{shell}mvnw.bat package` (Windows) in order to build the hardening
classes and get wrapper scripts for simple usage. Then, there will be
wrapper scripts in `target/bin/`.

You can run the wrapper scripts from anywhere on your computer since
they only contain absolute paths.


## Usage

In order to restrict access to files and subfolders of the folders
`projects` and `src/xsl` in your home folder run

```{shell}
export SAXON_ALLOWED_PATHS=~/projects,~/src/xsl
```

Then use the wrapper scripts in `target/bin/` and the Saxon
configuration in `saxon.xml`. This configuration file tells Saxon to
use URI resolvers for the various ways of accessing the file
system. These URI resolvers restrict access to the allowed paths.

In order to run XSLT with restriction to these paths, use the
configuration file like so:

```{shell}
PATH-TO/target/bin/xslt.sh -config:PATH-TO/saxon.xml -xsl:~/src/xsl/my.xsl -s:doc.xsl
```

Parameters are exactly the same as for the [Saxon command line
tool](https://www.saxonica.com/documentation10/index.html#!using-xsl/commandline).

Note, that the file system locations given as command line parameters
are not affected by the restricted access to the file system, but only
imported or included stylesheets, documents read with `doc()` etc. or
with `unparsed-text()`, and locations written to with
`<xsl:result-document>`.

When trying to access a location outside of the allowed paths, errors
like the following are thrown:

```{txt}
Error in xsl:result-document/@href on line 19 column 64 of add-key.xsl:
   path not allowed: /home/clueck/.ssh/authorized_keys
```


## License

MIT
