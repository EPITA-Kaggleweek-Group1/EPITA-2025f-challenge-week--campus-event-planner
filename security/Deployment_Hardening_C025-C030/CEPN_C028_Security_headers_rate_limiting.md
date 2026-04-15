
## Version info disclosed via `X-Powered-By` header

@app.after_request
def clean_headers(response):
    response.headers.pop("X-Powered-By", None)
    response.headers.pop("Server", None)
    return response


