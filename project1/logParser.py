log_file_path = '/Users/krishna_mac_pro/Downloads/myLogFile.txt'

def process_log(log_path):
    tj_values = []
    ts_values = []

    try:
        with open(log_path, 'r') as log_file:
            lines = log_file.readlines()
            for idx, line in enumerate(lines):
                if "JDBC Execution Time" in line:
                    tj_values.append(int(line.split(': ')[1].strip()))
                elif "Servlet Execution Time" in line:
                    ts_values.append(int(line.split(': ')[1].strip()))

    except FileNotFoundError:
        print(f"File {log_path} not found")
        return None, None

    return tj_values, ts_values

def calculate_average(values):
    if values:
        return sum(values) / len(values)
    return None

tj_values, ts_values = process_log(log_file_path)

avg_tj = calculate_average(tj_values)
avg_ts = calculate_average(ts_values)

if avg_tj is not None and avg_ts is not None:
    print(f"Average TJ: {avg_tj/1000000.0} ms")
    print(f"Average TS: {avg_ts/1000000.0} ms")
else:
    print("Error processing log file")
