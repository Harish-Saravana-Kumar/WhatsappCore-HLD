import re

with open('backend/src/main/java/com/whatsapp/RestServer.java', 'r') as f:
    content = f.read()

# Replace all occurrences of OPTIONS handlers
pattern = r'if \("OPTIONS"\.equals\(exchange\.getRequestMethod\(\)\)\) \{\s*exchange\.sendResponseHeaders\(200, -1\);\s*exchange\.close\(\);\s*return;'
replacement = 'if ("OPTIONS".equals(exchange.getRequestMethod())) {\n                handleOptions(exchange);\n                return;'

content = re.sub(pattern, replacement, content)

with open('backend/src/main/java/com/whatsapp/RestServer.java', 'w') as f:
    f.write(content)

print("✓ All OPTIONS handlers updated!")
