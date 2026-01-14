import { Component, inject } from '@angular/core';
import { ClientService } from '../../services/client-service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-ajouter',
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './ajouter.html',
  styleUrl: './ajouter.scss',
})
export class Ajouter {

  private fb = inject(FormBuilder);
  private clientService = inject(ClientService);
  private router = inject(Router);
  
  clientForm: FormGroup;
  isLoading = false;
  message = '';
  isError = false;

  constructor() {
    this.clientForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      telephone: [''],
      dateNaissance: [''],
      sexe: [''],
      adresse: [''],
      nationalite: ['']
    });
  }

  onSubmit(): void {
    if (this.clientForm.valid) {
      this.isLoading = true;
      this.message = '';
      this.isError = false;
      
      // Nettoie les données
      const donneesNettoyees = this.nettoyerDonnees(this.clientForm.value);
      
      this.clientService.createClient(donneesNettoyees)
        .subscribe({
          next: (reponse) => {
            this.isLoading = false;
            this.message = 'Client ajouté avec succès !';
            this.isError = false;
            
            this.clientForm.reset();
            
            // Redirection après 2 secondes
            setTimeout(() => {
              this.router.navigate(['/admin/clients']);
            }, 2000);
          },
          error: (erreur) => {
            this.isLoading = false;
            this.isError = true;
            
            if (erreur.error && erreur.error.message) {
              this.message = erreur.error.message;
            } else {
              this.message = 'Erreur lors de l\'ajout du client';
            }
          }
        });
    }
  }

  private nettoyerDonnees(data: any): any {
    const nettoye: any = {};
    for (const key in data) {
      nettoye[key] = data[key] === '' ? null : data[key];
    }
    return nettoye;
  }
}