/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.utils;

import client.uicomponents.Alerts;
import com.google.inject.Inject;
import commons.*;
import commons.primary_keys.DebtPK;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


public class ServerUtils {

	private final String server;
	private final Config config;

	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
	private static final ExecutorService EXECUTOR1 = Executors.newSingleThreadExecutor();
	private static final ExecutorService EXECUTOR2 = Executors.newSingleThreadExecutor();
	private static final ExecutorService EXECUTOR3 = Executors.newSingleThreadExecutor();
	@Inject
	public ServerUtils(Config config) throws IOException {
		this.config = config;
		this.server = config.getHttpServer();
	}

	public void handleConnectionException(Exception ex) {
		if (ex instanceof ProcessingException) {
			Alerts.connectionRefusedAlert();
		} else {
			Alerts.exceptionAlert(ex);
		}
	}

	public Event getEvent(UUID eventId) {
		try {
			return ClientBuilder
					.newClient(new ClientConfig())
					.target(config.getHttpServer())
					.path("/api/events/" + eventId)
					.request(APPLICATION_JSON)
					.accept(APPLICATION_JSON)
					.get(Event.class);
		} catch (Exception ex) {
			handleConnectionException(ex);
			return null;
		}
	}

	public Event addEvent(Event event) {
		try {
		return ClientBuilder.newClient(new ClientConfig())
				.target(server)
				.path("/api/events/")
				.request(APPLICATION_JSON)
				.accept(APPLICATION_JSON)
				.post(Entity.entity(event, APPLICATION_JSON), Event.class);
		} catch (Exception ex) {
			handleConnectionException(ex);
			return null;
		}
	}

	public List<Event> getEvents() {
		try {
		return ClientBuilder.newClient(new ClientConfig())
				.target(server)
				.path("/api/events/")
				.request(APPLICATION_JSON)
				.accept(APPLICATION_JSON)
				.get(new GenericType<List<Event>>() {});
		} catch (Exception ex) {
			handleConnectionException(ex);
			return null;
		}
	}

	public List<Expense> getExpenses(UUID eventId) {
		return ClientBuilder.newClient(new ClientConfig())
				.target(server)
				.path("/api/events/{eventId}/expenses")
				.resolveTemplate("eventId", eventId)
				.request(APPLICATION_JSON)
				.accept(APPLICATION_JSON)
				.get(new GenericType<List<Expense>>() {});
	}

	public Event updateEvent(UUID id, Event event) {
		try {
		return ClientBuilder.newClient(new ClientConfig())
				.target(server)
				.path("/api/events/" + id)
				.request(APPLICATION_JSON)
				.accept(APPLICATION_JSON)
				.put(Entity.entity(event, APPLICATION_JSON), Event.class);
		} catch (Exception ex) {
			handleConnectionException(ex);
			return null;
		}
	}

	public List<Participant> getParticipants(UUID eventId) {
		try {
			return ClientBuilder.newClient(new ClientConfig())
					.target(server)
					.path("/api/events/{eventId}/participants")
					.resolveTemplate("eventId", eventId)
					.request(APPLICATION_JSON)
					.accept(APPLICATION_JSON)
					.get(new GenericType<List<Participant>>() {});
		} catch (Exception ex) {
			handleConnectionException(ex);
			return null;
		}
	}

	public Participant addParticipant(Participant participant, UUID eventID) {
        try {
            return ClientBuilder.newClient(new ClientConfig())
                    .target(server)
                    .path("/api/events/" + eventID + "/participants/")
                    .request(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .post(Entity.entity(participant, APPLICATION_JSON), Participant.class);
        } catch (Exception ex) {
            handleConnectionException(ex);
            return null;
        }
	}

	public Participant getParticipant(UUID eventId, UUID id) {
        try {
            return ClientBuilder
                    .newClient(new ClientConfig())
                    .target(server)
                    .path("/api/events/" + eventId + "/participants/" + id)
                    .request(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .get(Participant.class);
        } catch (Exception ex) {
            handleConnectionException(ex);
            return null;
        }
	}

	public Participant updateParticipant(Participant participant, UUID eventId, UUID id) {
		try {
            return ClientBuilder.newClient(new ClientConfig())
                    .target(server)
                    .path("/api/events/" + eventId + "/participants/" + id)
                    .request(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .put(Entity.entity(participant, APPLICATION_JSON), Participant.class);
        } catch (Exception ex) {
            handleConnectionException(ex);
            return null;
        }
	}

	public Response deleteParticipant(UUID eventId, UUID id) {
		try {
			return ClientBuilder.newClient(new ClientConfig())
					.target(server)
					.path("/api/events/{eventId}/participants/{id}")
					.resolveTemplate("eventId", eventId)
					.resolveTemplate("id", id)
					.request()
					.delete();
		} catch (Exception ex) {
			handleConnectionException(ex);
			return null;
		}
	}

	public List<Debt> getDebts(Event event) {
		try {
			return ClientBuilder.newClient(new ClientConfig())
					.target(server)
					.path("/api/events/" + event.getId() + "/debts")
					.request(APPLICATION_JSON)
					.accept(APPLICATION_JSON)
					.get(new GenericType<List<Debt>>() {});
		} catch (Exception ex) {
			handleConnectionException(ex);
			return null;
		}
	}

	public Double settleDebt(UUID eventId, DebtPK debtPK, Double amount) {
		try {
			return ClientBuilder.newClient(new ClientConfig())
					.target(server)
					.path("/api/events/{eventId}/debts/settle/{payerId}/{debtorId}")
					.resolveTemplate("eventId", eventId)
					.resolveTemplate("payerId", debtPK.getPayerId())
					.resolveTemplate("debtorId", debtPK.getDebtorId())
					.request(APPLICATION_JSON)
					.post(Entity.entity(amount, APPLICATION_JSON), Double.class);
		} catch (Exception ex) {
			handleConnectionException(ex);
			return null;
		}
	}

	public Response recalculateDebt(UUID eventId) {
		try {
			return ClientBuilder.newClient(new ClientConfig())
					.target(server)
					.path("/api/events/{eventId}/debts/recalculate")
					.resolveTemplate("eventId", eventId)
					.request()
					.get();
		} catch (Exception ex) {
			handleConnectionException(ex);
			return null;
		}
	}

	public Expense addExpense(UUID eventID, Expense expense) {
		try {
			return ClientBuilder.newClient(new ClientConfig())
					.target(server)
					.path("/api/events/" + eventID + "/expenses/")
					.request(APPLICATION_JSON)
					.accept(APPLICATION_JSON)
					.post(Entity.entity(expense, APPLICATION_JSON), Expense.class);
		} catch (Exception ex) {
			handleConnectionException(ex);
			return null;
		}
	}
	public Response deleteExpense(UUID eventID, UUID expenseId) {
		try {
			return ClientBuilder.newClient(new ClientConfig())
					.target(server)
					.path("/api/events/{eventId}/expenses/{expenseId}")
					.resolveTemplate("eventId", eventID)
					.resolveTemplate("expenseId", expenseId)
					.request(APPLICATION_JSON)
					.accept(APPLICATION_JSON)
					.delete();
		} catch (Exception ex) {
			handleConnectionException(ex);
			return null;
		}
	}

	public void registerForUpdates(UUID eventId, Consumer<Expense> consumer) {
		EXECUTOR.submit(() -> {
				while (!Thread.interrupted()) {
					var response = ClientBuilder.newClient(new ClientConfig())
							.target(server)
							.path("/api/events/{eventId}/expenses/updates/create")
							.resolveTemplate("eventId", eventId)
							.request(APPLICATION_JSON)
							.accept(APPLICATION_JSON)
							.get(Response.class);
					if (response.getStatus() == HttpStatus.NO_CONTENT.value()) {
						continue;
					}
					var expense = response.readEntity(Expense.class);
					consumer.accept(expense);
				}
			});
	}
	public void registerForEditUpdates(UUID eventId, Consumer<Expense> consumer) {
		EXECUTOR1.submit(() -> {
			while (!Thread.interrupted()) {
				var response = ClientBuilder.newClient(new ClientConfig())
						.target(server)
						.path("/api/events/{eventId}/expenses/updates/edit")
						.resolveTemplate("eventId", eventId)
						.request(APPLICATION_JSON)
						.accept(APPLICATION_JSON)
						.get(Response.class);
				if (response.getStatus() == HttpStatus.NO_CONTENT.value()) {
					continue;
				}
				var expense = response.readEntity(Expense.class);
				consumer.accept(expense);
			}
		});
	}
	public void registerForDeleteUpdates(UUID eventId, Consumer<UUID> consumer) {
		EXECUTOR2.submit(() -> {
			while (!Thread.interrupted()) {
				var response = ClientBuilder.newClient(new ClientConfig())
						.target(server)
						.path("/api/events/{eventId}/expenses/updates/delete")
						.resolveTemplate("eventId", eventId)
						.request(APPLICATION_JSON)
						.accept(APPLICATION_JSON)
						.get(Response.class);
				if (response.getStatus() == HttpStatus.NO_CONTENT.value()) {
					continue;
				}
				var expense = response.readEntity(UUID.class);
				consumer.accept(expense);
			}
		});
	}

	public void registerForTagUpdates(UUID eventId, Consumer<Tag> consumer) {
		EXECUTOR3.submit(() -> {
			while (!Thread.interrupted()) {
				var response = ClientBuilder.newClient(new ClientConfig())
						.target(server)
						.path("/api/events/{eventId}/tags/updates")
						.resolveTemplate("eventId", eventId)
						.request(APPLICATION_JSON)
						.accept(APPLICATION_JSON)
						.get(Response.class);
				if (response.getStatus() == HttpStatus.NO_CONTENT.value()) {
					continue;
				}
				var tag = response.readEntity(Tag.class);
				consumer.accept(tag);
			}
		});
	}
	public void stop() {
		EXECUTOR.shutdownNow();
		EXECUTOR1.shutdownNow();
		EXECUTOR2.shutdownNow();
		EXECUTOR3.shutdownNow();
	}

	public Expense updateExpense(UUID eventId, UUID expenseId, Expense expense) {
		try {
			return ClientBuilder.newClient(new ClientConfig())
					.target(server)
					.path("/api/events/{eventId}/expenses/{expenseId}")
					.resolveTemplate("eventId", eventId)
					.resolveTemplate("expenseId", expenseId)
					.request(APPLICATION_JSON)
					.accept(APPLICATION_JSON)
					.put(Entity.entity(expense, APPLICATION_JSON), Expense.class);
		} catch (Exception ex) {
			handleConnectionException(ex);
			return null;
		}
	}

	public List<Tag> getTags(UUID eventId) {
		try {
			return ClientBuilder
					.newClient(new ClientConfig())
					.target(server)
					.path("/api/events/" + eventId + "/tags")
					.request(APPLICATION_JSON)
					.accept(APPLICATION_JSON)
					.get(new GenericType<List<Tag>>() {});
		} catch (Exception ex) {
			handleConnectionException(ex);
			return null;
		}
	}

	public Tag getTag(UUID eventId, UUID tagId) {
		try {
			return ClientBuilder
					.newClient(new ClientConfig())
					.target(server)
					.path("/api/events/" + eventId + "/tags/" + tagId)
					.request(APPLICATION_JSON)
					.accept(APPLICATION_JSON)
					.get(Tag.class);
		} catch (Exception ex) {
			handleConnectionException(ex);
			return null;
		}
	}

	public Tag updateTag(UUID eventId, UUID tagId, Tag newTag) {
		try {
			return ClientBuilder.newClient(new ClientConfig())
					.target(server)
					.path("/api/events/" + eventId + "/tags/" + tagId)
					.request(APPLICATION_JSON)
					.accept(APPLICATION_JSON)
					.put(Entity.entity(newTag, APPLICATION_JSON), Tag.class);
		} catch (Exception ex) {
			handleConnectionException(ex);
			return null;
		}
	}

	public Tag deleteTag(UUID eventId, UUID tagId) {
		try {
			return ClientBuilder
					.newClient(new ClientConfig())
					.target(server)
					.path("/api/events/" + eventId + "/tags/" + tagId)
					.request(APPLICATION_JSON)
					.accept(APPLICATION_JSON)
					.delete(Tag.class);
		} catch (Exception ex) {
			handleConnectionException(ex);
			return null;
		}
	}
	public Tag addTag(UUID eventId, Tag tag) {
		try {
			return ClientBuilder
					.newClient(new ClientConfig())
					.target(server)
					.path("/api/events/" + eventId + "/tags/")
					.request(APPLICATION_JSON)
					.accept(APPLICATION_JSON)
					.post(Entity.entity(tag, APPLICATION_JSON), Tag.class);
		} catch (Exception ex) {
			handleConnectionException(ex);
			return null;
		}
	}
}