## Phase 3 update

### File upload on heroku server though POST /upload 
server adress: https://limitless-ocean-62391.herokuapp.com
limitation: 
1) can upload file and get FileId, but file object is not linked to a message.
2) file size is bigger than around 3 page, then it causes socket timeout error.

### Download byteArray object of files through GET /file/:fileId
limitation: can download bytes, but convert it into original file.

### Please Runs the app in the development mode by npm install; npm start.
Please Open [http://localhost:3000](http://localhost:3000) to view it in the browser.
