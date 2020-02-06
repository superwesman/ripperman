# super/ripperman

A Clojure library that wraps [makemkvcon](https://www.makemkv.com/)

## Usage

```clojure
(read-info! {:executable "/Applications/MakeMKV.app/Contents/MacOS/makemkvcon"
             :disc 0})

(rip! {:executable "/Applications/MakeMKV.app/Contents/MacOS/makemkvcon"
       :disc 0})
```

## License

Copyright © 2020
