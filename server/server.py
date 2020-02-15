# server.py `source bin/activate`
from flask import Flask, render_template
from flask_cors import CORS
app = Flask(__name__, static_folder="../static/dist",
            template_folder="../static")
CORS(app)

@app.route("/")
def index():
    # return render_template("index.html")
    return 'This is Team Tin and we out here'


@app.route("/hello")
def hello():
    return 'Hello World'


if __name__ == "__main__":
    app.run()
