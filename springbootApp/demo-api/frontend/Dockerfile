FROM node:20-alpine

WORKDIR /app

# Copy package files first to speed up future builds
COPY package*.json ./
RUN npm install

# Copy the rest of your React code
COPY . .

EXPOSE 5173

# Vite requires the --host flag to be visible outside the container
CMD ["npm", "run", "dev", "--", "--host"]