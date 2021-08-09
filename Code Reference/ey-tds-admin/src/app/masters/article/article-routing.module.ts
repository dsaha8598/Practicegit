import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ArticleComponent } from './article.component';
import { ArticleformComponent } from './articleform/articleform.component';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';

const routes: Routes = [
  {
    path: '',
    component: ArticleComponent
  },
  {
    path: 'form',
    component: ArticleformComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ArticleRoutingModule {
  constructor(private logger: CustomLoggerService) {
    this.logger.debug(routes);
  }
}
