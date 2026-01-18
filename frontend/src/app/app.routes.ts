import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Register } from './register/register';
import { AdminLayout } from './admin/layout/admin-layout/admin-layout';
import { Dashboard } from './admin/dashboard/dashboard';
import { Clients } from './admin/clients/clients';
import { Comptes } from './admin/comptes/comptes';
import { Ajouter } from './admin/ajouter/ajouter';
import { ClientLayout } from './client/layout/client-layout/client-layout';
import { DashboardClient } from './client/dashboard-client/dashboard-client';
import { ComptesClient } from './client/comptes-client/comptes-client';
import { TransactionsClient } from './client/transactions-client/transactions-client';
import { AjouterCompte } from './client/ajouter-compte/ajouter-compte';

export const routes: Routes = [
    
    {path: 'login', component: Login },
    {path:'register', component: Register},

    {path: 'admin',
        component: AdminLayout,
        children: [
            { path: 'dashboard', component: Dashboard },
            { path: 'clients', component: Clients,},
            { path: 'comptes', component: Comptes },
            { path: 'clients/ajouter', component: Ajouter },
            { path: 'comptes/ajouter/:id', component: AjouterCompte,},
        ]
    },

    {path: 'client',
        component: ClientLayout,
        children: [
            { path: 'dashboard', component: DashboardClient },
            { path: 'comptes', component: ComptesClient,},
            { path: 'transactions', component: TransactionsClient },
        ]
    },

    { path: '', redirectTo: '/login', pathMatch: 'full' },
    { path: '**', redirectTo: '/login' },

];
