# Architecture Overview

## Goal

Build a modular Amazon-like commerce platform with a React frontend and a Spring Boot microservices backend.

## Frontend

- React 19
- Redux Toolkit for session, cart, and order state
- React Router for navigation
- Axios-ready service layer
- Responsive UI with reusable cards, panels, and action bars

## Backend target

- Auth, user, product, inventory, cart, order, payment, and notification services
- Eureka for discovery
- API Gateway for routing and cross-cutting concerns
- Kafka for events and orchestration

## Frontend strategy

- Start with a custom-built shell and route structure
- Use mock data and local persistence until APIs are connected
- Keep service adapters isolated so backend integration is a swap, not a rewrite
