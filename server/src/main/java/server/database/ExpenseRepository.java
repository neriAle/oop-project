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
package server.database;

import commons.Expense;
import commons.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    Integer deleteExpenseById(UUID id);
    Collection<Expense> findExpenseByEventId(UUID eventId);
    Collection<Expense> findExpenseByPayerId(UUID id);
    Collection<Expense> findExpenseByPayer(Participant payer);
    Collection<Expense> findExpenseByDebtorsContaining(Participant debtor);
}